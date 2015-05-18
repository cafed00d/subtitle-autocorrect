Auto-corrects spelling mistakes made by
[SupRip](http://www.videohelp.com/tools/SupRip) when it converts a
`.sub` int a `.srt` file. The most common mistake is
that `SupRip` confuses lower case `l` with upper case `I` which is apparent
with dialog words like `Isn't` (which gets converted to `lsn't`) and in
descriptive terms that appear in all upper case letters within brackets, such as `[SlNGlNG]`.

This utility takes several approaches to correcting these mistakes:

  1. If a word is mostly upper case except for lower case `l`s, then the lower case `l`s are converted to upper case `I`s. (Version 1.0)
  1. If a word is mostly lower case letters except for upper case `I`s appearing in any position other than the first letter, then the offending upper case `I`s are converted to lower case `l`s. (Version 1.0)
  1. If a word appears in the common mistakes list, it is corrected. The common mistakes list can be found in the autocorrect.properties file that comes with the utility. It is in the JAR file but you can extract it into the utilities base directory and edit that file to provide for additional cases. (Version 1.0)
  1. If a word starts with a lower case `l` and a consonant or apostrophe, the lower case `l` is converted to an upper case `I`. (Version 1.1)
  1. If a word starts with an upper case `I` and a vowel, the upper case `I` is converted to a lower case `l`. (Version 1.1)
  1. If a word ends with an apostrophe and two upper case `I`s, the upper case `I`s are converted to lower case `l`s. (Version 1.1)

Refer to the autocorrect.bat file for usage instructions.