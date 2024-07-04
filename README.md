# Ask CLI
A simple cli tool that make requests to LLMs.

# Usage
```bash
# Translate a file into chinese and return immediately.
$ cat ./article | ask -o "Translate these into chinese"

# Ask a question and enter interactive mode
$ ask "nodejs axios send http request example"
......Examples........
>> (your turn)

# This is not supported :(
$ cat ./article | ask -o "Translate these into chinese"
```