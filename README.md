# Domain Checker

High-fidelity domain resolution tool optimized for uBO filter lists, hostnames, and cosmetic/network rules.

By **BlazeFTL**

## Features

- Cleans and optimizes raw domain lists or full uBlock Origin cosmetic/network filter lists
- Resolves DNS via Google DoH (DNS-over-HTTPS), bypassing carrier/ISP DNS hijacking
- Parallel live checks to detect dead/unreachable hosts fast
- Keeps at least one backup server alive when an entire filter group goes dead
- Outputs in multiple formats: comma-separated, pipe-separated, and processed uBO filter syntax
- Fullscreen view + one-click copy for large outputs
- Personalization: accent palettes, gradients, font style (monospace/sans/serif)

<img width="702" height="1560" alt="Screenshot_20260618-112205_Spark Launcher" src="https://github.com/user-attachments/assets/0917c393-a4ce-402c-8e70-fa7fc02d16ac" />
<img width="702" height="1560" alt="Screenshot_20260618-112208_Spark Launcher" src="https://github.com/user-attachments/assets/de0991f5-e06a-4408-b3eb-55f385b6eade" />
<img width="702" height="1560" alt="Screenshot_20260618-105617_Spark Launcher" src="https://github.com/user-attachments/assets/1804c2ca-4e29-4da8-89ac-d121655826ec" />
<img width="702" height="1560" alt="Screenshot_20260618-112324_Spark Launcher" src="https://github.com/user-attachments/assets/7aa252d3-587b-4d54-b969-b7894e9479e4" />


## Usage

1. Paste domains or uBO filter rules into the input box, or upload a `.txt` file
2. Click **RUN ANALYSIS**
3. Watch live verification progress in the queue log
4. Browse results:
   - **Dead Domains**
   - **Live (comma)**
   - **Live (pipe)**
   - **Processed uBlockOrigin Filters**
5. Copy or download any output block

## Demo

| Input & Analysis | Results |
|---|---|
| Paste/upload filter rules, run live DoH checks | Dead/live domain breakdown + processed filter output |

## License

MIT
