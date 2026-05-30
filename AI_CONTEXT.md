# AI Assistant Context

This file contains guidance for AI assistants (Perplexity, Claude, Cursor, etc.) working in this repository.

## File Reading Strategy

When reading multiple markdown or source files in this repo:

1. **Always read files individually**, not concatenated via `cat file1 file2`. Use separate `read_file` calls per file to avoid output truncation.
2. **Check line counts first** for any file that may be large. Run `wc -l <file>` before reading. For files over ~150 lines, read in chunks using `start_line`/`end_line` parameters.
3. **Parallelize independent reads** — kick off multiple file reads simultaneously rather than sequentially.

These rules apply especially to the `documentation/` directory, where architecture docs can be several hundred lines each.
