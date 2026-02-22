#!/bin/bash

OUTPUT_FILE="docs/user_stories.md"

rm -f "$OUTPUT_FILE"

find . -type f -name "*.feature" \
    -not -path "*/target/*" \
    -not -path "*/build/*" \
    -not -path "*/bin/*" \
    -not -path "*/.git/*" \
    -not -path "*/.gradle/*" \
    | sort -u | while read -r file; do

    filename=$(basename "$file")

    echo "## $filename" >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"
    echo '```gherkin' >> "$OUTPUT_FILE"
    cat "$file" >> "$OUTPUT_FILE"
    echo '```' >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"

done