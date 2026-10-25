import sys
import xml.etree.ElementTree as ET

report = ET.parse(sys.argv[1]).getroot()
for kind in ("LINE", "INSTRUCTION", "BRANCH"):
    counter = next(node for node in report.findall("counter") if node.attrib["type"] == kind)
    covered = int(counter.attrib["covered"])
    missed = int(counter.attrib["missed"])
    total = covered + missed
    percent = 100 * covered / total if total else 100
    print(f"{kind.title()} coverage: **{percent:.2f}%** ({covered}/{total})")
