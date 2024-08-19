# Episode Sort

Small program that organizes downloaded episodes from a show into a 
better maintainable structure.

## Output
<p>
A show with <code>n</code> seasons and <code>n</code> episodes each will be 
rearranged into the following structure:
</p>

```
Showname/
├── Season 01/
│   ├── Showname s01e01.mkv
│   ├── Showname s01e02.mkv
│   ├── ...
│   └── Showname s01e(n).mkv
├── Season 02/
│   ├── Showname s02e01.mkv
│   └── ...
├── Season 03/
├── ...
└── Season (n)/
    ├── Showname s(n)e01.mkv
    ├── ...
    └── Showname s(n)e(n).mkv
```



## Requirements
For the program to work properly, there are some requirements that need to be met.
- season- and episode-numbers < 10 are padded with a single leading 0 
- episodes in a season must begin with the first, then the second etc. <br>
(so `e01, e02, ..., e(n)` is allowed while `e01, e03` or `e32` isn't)
- all episodes must:
  - be in *.mkv* format
  - be ordered by episode numbers
  - have `S|s<season-number>` somewhere in their name   
   (except if there's only 1 
    season)


