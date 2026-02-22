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
    display_name=$(echo "${filename//./ }" | awk '{for(i=1;i<=NF;i++) $i=toupper(substr($i,1,1)) substr($i,2); print}')

    echo "## $display_name" >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"
    echo '```gherkin' >> "$OUTPUT_FILE"
    cat "$file" >> "$OUTPUT_FILE"
    printf '\n```\n\n' >> "$OUTPUT_FILE"

done