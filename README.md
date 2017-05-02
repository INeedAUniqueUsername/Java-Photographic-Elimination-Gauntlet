# Java-Photographic-Elimination-Gauntlet
Also known as JPEG.

This is where you will find random image manipulation tools such as the Transcendence Image Generator.

## Transcendence Image Generator
This program was made to manipulate various image resources from the game Transcendence. It takes all the images from a directory and its subfolders and applies a specific algorithm to each one. It outputs all the modified images to a folder called Transcendence Image Generator Output, and this folder is located in the same directory as where you put the JAR file. You can also download a modified source of Transcendence that overwrites all images with their Pencil Sketch versions
- Pencil Sketch: Uses edge detection to make images look like simple black-and-white pencil sketches. This algorithm, called "RGB Ratio," takes the "Total RGB" of a pixel by adding up all of that pixel's color channel values and compares it to the Total RGB of pixels to the right, bottom-right, and bottom. If there are at least two adjacent pixels whose Total RGB differs by at least 60%, then this pixel will be dark on the resulting image. Otherwise it will be blank.
- Color Swap: Randomly swaps the color channels of each pixel (i.e. RGB swapped to BRG or GBR). Can be used to create palette swaps of images
