#!/bin/bash
# Script to build and serve the MicroProfile Tutorial documentation
# with automatic rebuilding when files change

# Check if inotifywait is installed (part of inotify-tools)
if ! command -v inotifywait &> /dev/null; then
    echo "inotify-tools is required but not installed. Installing..."
    apt-get update && apt-get install -y inotify-tools || {
        echo "Failed to install inotify-tools. Please install it manually."
        exit 1
    }
fi

# Check if a simple HTTP server is available
if ! command -v python3 &> /dev/null; then
    echo "Python 3 is required but not installed. Please install it."
    exit 1
fi

# Function to build the site
build_site() {
    echo "Building documentation site..."
    antora antora-assembler.yml && ./fix-edit-links.sh
    echo "Build completed!"
}

# Function to serve the site
serve_site() {
    echo "Starting HTTP server on port 8080..."
    echo "Open your browser at http://localhost:8080"
    echo "Press Ctrl+C to stop"
    (cd build/site && python3 -m http.server 8080)
}

# Function to watch for file changes
watch_files() {
    echo "Watching for file changes..."
    
    # Build once initially
    build_site
    
    # Watch for changes in AsciiDoc files, YAML configs, and UI files
    while true; do
        inotifywait -r -e modify,create,delete \
            --include '.*\.adoc$|.*\.yml$|.*\.hbs$|.*\.css$|.*\.js$' \
            ./modules ./supplemental-ui ./*.yml
            
        # Rebuild when changes are detected
        echo "Changes detected, rebuilding..."
        build_site
    done
}

# Main script logic
case "$1" in
    build)
        build_site
        ;;
    serve)
        build_site
        serve_site
        ;;
    watch)
        # Start the HTTP server in the background
        (serve_site) &
        server_pid=$!
        
        # Trap Ctrl+C to kill the server process
        trap "kill $server_pid; exit" INT TERM
        
        # Watch for file changes and rebuild
        watch_files
        ;;
    *)
        echo "Usage: $0 {build|serve|watch}"
        echo ""
        echo "Commands:"
        echo "  build    Build the documentation site"
        echo "  serve    Build and serve the site locally"
        echo "  watch    Build, serve, and automatically rebuild on changes"
        exit 1
        ;;
esac

exit 0
