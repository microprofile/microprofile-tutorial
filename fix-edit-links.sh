#!/bin/bash
# Script to fix Edit This Page links in Antora site
# This script replaces local file:// links with GitHub repository links

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Base directory for the site
SITE_DIR="/workspaces/microprofile-tutorial/build/site"

# GitHub repository information
REPO_URL="https://github.com/microprofile/microprofile-tutorial"
BRANCH="main"
PATH_PREFIX="modules/ROOT/pages/"

# Check if the site directory exists
if [ ! -d "$SITE_DIR" ]; then
    echo -e "${RED}Error: Site directory not found at $SITE_DIR${NC}"
    echo -e "${YELLOW}Did you run 'antora antora-assembler.yml' first?${NC}"
    exit 1
fi

echo -e "${BLUE}Fixing Edit This Page links in HTML files...${NC}"

# Count of files processed and fixed
total_files=0
fixed_files=0

# Find all HTML files and process them
find "$SITE_DIR" -name "*.html" -type f | while read -r file; do
    ((total_files++))
    
    # Check if the file contains any file:// links that need fixing
    if grep -q "file:///workspaces/microprofile-tutorial/\./modules/ROOT/pages/" "$file"; then
        # Replace file:// links with GitHub links
        sed -i "s|file:///workspaces/microprofile-tutorial/\./modules/ROOT/pages/|${REPO_URL}/edit/${BRANCH}/${PATH_PREFIX}|g" "$file"
        ((fixed_files++))
        echo -e "${GREEN}✓${NC} Fixed links in: ${YELLOW}$(basename "$file")${NC}"
    fi
done

# Summary
echo -e "\n${BLUE}Summary:${NC}"
echo -e "  - Total HTML files processed: ${total_files}"
echo -e "  - Files with links fixed: ${fixed_files}"
echo -e "\n${GREEN}Edit links successfully fixed!${NC}"

# Provide hint about repository URL
echo -e "\n${YELLOW}Note:${NC} If you change the repository URL or branch, update the following in this script:"
echo -e "  - REPO_URL (currently: ${REPO_URL})"
echo -e "  - BRANCH (currently: ${BRANCH})"
