# Episode Sort

Small program that organizes downloaded episodes from a series into a 
better maintainable structure.

## Output
<p>
A series with <code>n</code> seasons and <code>n</code> episodes each will be 
rearranged into the following structure:
</p>

``` 
Series/
├── Season 01/
│   ├── Series s01e01.mkv
│   ├── Series s01e02.mkv
│   ├── ...
│   └── Series s01e(n).mkv
├── Season 02/
│   ├── Series s02e01.mkv
│   └── ...
├── Season 03/
├── ...
└── Season (n)/
    ├── Series s(n)e01.mkv
    ├── ...
    └── Series s(n)e(n).mkv
```



## Requirements
For the program to work properly, there are some requirements that need to be met.
- season- and episode-numbers < 10 are padded with a single leading 0 
- episodes in a season must begin with the first, then the second etc. <br>
(so `e01, e02, ..., e(n)` is allowed while `e01, e03` or `e32` isn't)
- all episodes must:
  - be in *.mkv* format
  - have `S|s<season-number>` somewhere in their name
  - be ordered by episode numbers


