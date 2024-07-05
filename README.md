# Ask CLI
A simple cli tool that make requests to LLMs.

# Usage
```bash
# Translate a file into chinese and return immediately.
$ cat ./article | ask "Translate these into chinese"

# Ask a question and enter interactive mode
$ ask -i "nodejs axios send http request example"
......Examples........
>> (your turn)

# Translate this article line-by-line (Same Context, may exhaust your token)
$ cat ./article | ask -i "Translate what I say to Chinese"

# Translate this article line-by-line (Independent context for each)
$ cat ./article | ask -d "Translate what I say to Chinese"

# Translate the whole passage in one time.
$ cat ./article | ask "Translate this into Chinese"
```