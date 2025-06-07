#!/bin/bash
# update-repo-url.sh - Updates repository URLs in configuration files
# This script detects the Git repository URL and branch and updates them in configuration files

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}Detecting Git repository information...${NC}"

# Get the remote repository URL
REPO_URL=$(git remote get-url origin 2>/dev/null)
if [ -z "$REPO_URL" ]; then
    echo -e "${YELLOW}Warning: Could not determine remote repository URL.${NC}"
    echo -e "${YELLOW}Using default URL: https://github.com/ttelang/microprofile-tutorial${NC}"
    REPO_URL="https://github.com/ttelang/microprofile-tutorial"
else
    # Convert SSH URL to HTTPS URL if needed
    if [[ $REPO_URL == git@github.com:* ]]; then
        REPO_URL="https://github.com/${REPO_URL#git@github.com:}"
    fi
    # Remove .git suffix if present
    REPO_URL=${REPO_URL%.git}
    echo -e "${GREEN}Repository URL: ${REPO_URL}${NC}"
fi

# Get the current branch
BRANCH=$(git symbolic-ref --short HEAD 2>/dev/null)
if [ -z "$BRANCH" ]; then
    echo -e "${YELLOW}Warning: Could not determine current branch.${NC}"
    echo -e "${YELLOW}Using default branch: main${NC}"
    BRANCH="main"
else
    echo -e "${GREEN}Current branch: ${BRANCH}${NC}"
fi

# Update antora.yml with the correct edit_url
echo -e "${BLUE}Updating antora.yml with repository information...${NC}"
EDIT_URL="${REPO_URL}/edit/${BRANCH}/modules/ROOT/pages/{path}"

# Use sed to update the edit_url in antora.yml
if [ -f "antora.yml" ]; then
    # Check if edit_url already exists
    if grep -q "^edit_url:" antora.yml; then
        # Replace existing edit_url
        sed -i "s|^edit_url:.*$|edit_url: ${EDIT_URL}|" antora.yml
    else
        # Add edit_url after version line
        sed -i "/^version:/a edit_url: ${EDIT_URL}" antora.yml
    fi
    echo -e "${GREEN}✓${NC} Updated edit_url in antora.yml to: ${YELLOW}${EDIT_URL}${NC}"
else
    echo -e "${RED}Error: antora.yml not found${NC}"
fi

# Update fix-edit-links.sh with the correct REPO_URL and BRANCH
echo -e "${BLUE}Updating fix-edit-links.sh with repository information...${NC}"
if [ -f "fix-edit-links.sh" ]; then
    # Update REPO_URL
    sed -i "s|^REPO_URL=.*$|REPO_URL=\"${REPO_URL}\"|" fix-edit-links.sh
    # Update BRANCH
    sed -i "s|^BRANCH=.*$|BRANCH=\"${BRANCH}\"|" fix-edit-links.sh
    echo -e "${GREEN}✓${NC} Updated repository information in fix-edit-links.sh"
else
    echo -e "${RED}Error: fix-edit-links.sh not found${NC}"
fi

echo -e "\n${GREEN}Repository information has been updated in configuration files.${NC}"
echo -e "You can now build the documentation with:"
echo -e "  ${YELLOW}antora antora-assembler.yml && ./fix-edit-links.sh${NC}"
