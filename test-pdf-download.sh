#!/bin/bash

echo "🔍 Testing PDF Download Functionality"
echo "======================================"

# Check if the site was built
if [ ! -d "build/site" ]; then
    echo "❌ Site not built. Run the GitHub Actions workflow first."
    exit 1
fi

echo "✅ Site directory exists"

# Check if PDF exists in assembler location
if [ -f "build/assembler/microprofile-tutorial/6.1/microprofile-tutorial.pdf" ]; then
    echo "✅ PDF found in assembler location"
    PDF_SIZE=$(stat -f%z "build/assembler/microprofile-tutorial/6.1/microprofile-tutorial.pdf" 2>/dev/null || stat -c%s "build/assembler/microprofile-tutorial/6.1/microprofile-tutorial.pdf")
    echo "   Size: ${PDF_SIZE} bytes"
else
    echo "❌ PDF not found in assembler location"
fi

# Check if PDF exists in site location (where download link points)
if [ -f "build/site/microprofile-tutorial/6.1/microprofile-tutorial.pdf" ]; then
    echo "✅ PDF found in download location: /microprofile-tutorial/6.1/microprofile-tutorial.pdf"
    PDF_SIZE=$(stat -f%z "build/site/microprofile-tutorial/6.1/microprofile-tutorial.pdf" 2>/dev/null || stat -c%s "build/site/microprofile-tutorial/6.1/microprofile-tutorial.pdf")
    echo "   Size: ${PDF_SIZE} bytes"
else
    echo "❌ PDF not found in download location: /microprofile-tutorial/6.1/microprofile-tutorial.pdf"
    echo "   This is where the download link expects to find the PDF"
fi

# Check if .htaccess files exist
if [ -f "build/site/.htaccess" ]; then
    echo "✅ Root .htaccess file exists"
else
    echo "❌ Root .htaccess file missing"
fi

if [ -f "build/site/microprofile-tutorial/6.1/.htaccess" ]; then
    echo "✅ PDF directory .htaccess file exists"
else
    echo "❌ PDF directory .htaccess file missing"
fi

# Check the main HTML file for download link
if [ -f "build/site/microprofile-tutorial/6.1/index.html" ]; then
    echo "✅ Main HTML file exists"
    
    # Look for PDF download link
    if grep -q "microprofile-tutorial.pdf" "build/site/microprofile-tutorial/6.1/index.html"; then
        echo "✅ PDF download link found in HTML"
        echo "   Link details:"
        grep -n "microprofile-tutorial.pdf" "build/site/microprofile-tutorial/6.1/index.html" | head -2
    else
        echo "❌ PDF download link not found in HTML"
    fi
    
    # Check if our JavaScript is included
    if grep -q "querySelectorAll.*pdf" "build/site/microprofile-tutorial/6.1/index.html"; then
        echo "✅ PDF download JavaScript found in HTML"
    else
        echo "❌ PDF download JavaScript not found in HTML"
    fi
else
    echo "❌ Main HTML file not found"
fi

echo ""
echo "🎯 Manual Test Instructions:"
echo "1. Open the deployed site in a browser"
echo "2. Navigate to the tutorial page"
echo "3. Click the 'Download PDF' button"
echo "4. The PDF should download as 'microprofile-tutorial.pdf'"
echo ""
echo "🔗 Expected PDF URL: [your-site-url]/microprofile-tutorial/6.1/microprofile-tutorial.pdf"
