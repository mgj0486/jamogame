#!/usr/bin/env python3
"""
국립국어원 한국어기초사전(krdict) XML 에서 자모 게임 출제 단어를 뽑는다.

목록은 두 가지를 만든다.
  words_N.txt : 출제용. 아는 단어만 나오도록 명사 + 초급/중급으로 좁힌다.
  valid_N.txt : 입력 검증용. 품사도 등급도 가리지 않고 최대한 넓게 담는다.

사용법:
    # 출제용
    python3 tools/extract_words.py <XML 폴더>
    # 검증용
    python3 tools/extract_words.py <XML 폴더> --prefix valid --pos "" --levels 초급 중급 고급 없음

XML 은 krdict.korean.go.kr 의 "사전 전체 내려받기" 또는 아래 미러에서 받는다.
    https://github.com/spellcheck-ko/korean-dict-nikl-krdict
라이선스: CC BY-SA 2.0 KR (출처 표기 + 동일조건변경허락)
"""
import argparse
import glob
import os
import re

BASE, LAST = 0xAC00, 0xD7A3
CHO = list("ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ")
JUNG = list("ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ")
JONG = [""] + list("ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ")

# 앱의 Hangul.kt 와 같은 규칙으로 쪼갠다. 둘이 어긋나면 게임이 깨지므로 함께 고쳐야 한다.
DOUBLED = {"ㄲ": "ㄱㄱ", "ㄸ": "ㄷㄷ", "ㅃ": "ㅂㅂ", "ㅆ": "ㅅㅅ", "ㅉ": "ㅈㅈ"}
COMPOUND_JONG = {
    "ㄳ": "ㄱㅅ", "ㄵ": "ㄴㅈ", "ㄶ": "ㄴㅎ", "ㄺ": "ㄹㄱ", "ㄻ": "ㄹㅁ", "ㄼ": "ㄹㅂ",
    "ㄽ": "ㄹㅅ", "ㄾ": "ㄹㅌ", "ㄿ": "ㄹㅍ", "ㅀ": "ㄹㅎ", "ㅄ": "ㅂㅅ",
}
COMPOUND_VOWEL = {
    "ㅐ": "ㅏㅣ", "ㅒ": "ㅑㅣ", "ㅔ": "ㅓㅣ", "ㅖ": "ㅕㅣ", "ㅘ": "ㅗㅏ", "ㅙ": "ㅗㅏㅣ",
    "ㅚ": "ㅗㅣ", "ㅝ": "ㅜㅓ", "ㅞ": "ㅜㅓㅣ", "ㅟ": "ㅜㅣ", "ㅢ": "ㅡㅣ",
}


def decompose(word):
    """한글 음절만으로 된 단어를 기본 자모 목록으로. 한글이 아니면 None."""
    out = []
    for ch in word:
        code = ord(ch)
        if not BASE <= code <= LAST:
            return None
        index = code - BASE
        cho = CHO[index // (21 * 28)]
        out += list(DOUBLED.get(cho, cho))
        jung = JUNG[index // 28 % 21]
        out += list(COMPOUND_VOWEL.get(jung, jung))
        jong = JONG[index % 28]
        for part in COMPOUND_JONG.get(jong, jong):
            out += list(DOUBLED.get(part, part))
    return out


FEAT = re.compile(r'<feat att="([^"]+)" val="(.*?)" ?/>')

# XML 파서를 쓰지 않는 이유: 배포 파일 일부에 이스케이프하지 않은 '<' 가 섞여 있어
# (외국어 뜻풀이 안의 <système ...>) well-formed 가 아니다. 형식이 한 줄 한 feat 로
# 아주 규칙적이라 줄 단위로 읽는 편이 안전하고 빠르다.
def entries(path):
    """XML 하나에서 (표제어, 단위, 품사, 등급) 을 흘려 읽는다."""
    record = None
    with open(path, encoding="utf-8") as f:
        for line in f:
            if "<LexicalEntry" in line:
                record = {"in_sense": False}
            elif record is None:
                continue
            elif "<Sense" in line:
                # 뜻풀이 안쪽은 표제어 정보가 아니다
                record["in_sense"] = True
            elif "</LexicalEntry>" in line:
                yield (
                    record.get("writtenForm"),
                    record.get("lexicalUnit"),
                    record.get("partOfSpeech"),
                    record.get("vocabularyLevel"),
                )
                record = None
            elif not record["in_sense"]:
                m = FEAT.search(line)
                # 같은 att 가 여러 번 나오면 앞의 것(표제어 쪽)을 쓴다
                if m and m.group(1) not in record:
                    record[m.group(1)] = m.group(2)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("xml_dir", help="krdict XML 파일들이 있는 폴더")
    parser.add_argument("--levels", nargs="*", default=["초급", "중급"],
                        help="포함할 어휘 등급 (초급/중급/고급/없음)")
    parser.add_argument("--jamo-counts", nargs="*", type=int, default=[5, 6])
    parser.add_argument("--pos", default="명사", help="품사. 빈 문자열이면 가리지 않는다")
    parser.add_argument("--prefix", default="words", help="파일 이름 앞부분 (words / valid)")
    parser.add_argument("--out", default="app/src/main/assets")
    args = parser.parse_args()

    levels = set(args.levels)
    picked = {n: set() for n in args.jamo_counts}
    seen = 0
    for path in sorted(glob.glob(os.path.join(args.xml_dir, "*.xml"))):
        for form, unit, pos, level in entries(path):
            seen += 1
            if not form or unit != "단어":
                continue
            if args.pos and pos != args.pos:
                continue
            if (level or "없음") not in levels:
                continue
            jamos = decompose(form)
            if jamos is None or len(jamos) not in picked:
                continue
            picked[len(jamos)].add(form)

    os.makedirs(args.out, exist_ok=True)
    for count, words in sorted(picked.items()):
        path = os.path.join(args.out, f"{args.prefix}_{count}.txt")
        with open(path, "w", encoding="utf-8") as f:
            f.write(f"# 자모 {count}개 {args.pos or '전체 품사'}. 국립국어원 한국어기초사전 (CC BY-SA 2.0 KR)\n")
            f.write(f"# 어휘등급: {', '.join(sorted(levels))} / tools/extract_words.py 로 생성\n")
            for word in sorted(words):
                f.write(word + "\n")
        print(f"{path}: {len(words)}개")
    print(f"표제어 {seen}개 훑음")


if __name__ == "__main__":
    main()
