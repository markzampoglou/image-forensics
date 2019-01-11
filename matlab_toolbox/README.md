# An Evaluation Framework for Image Tampering Localization 

## Introduction

This is a MATLAB framework for the evaluation of image tampering localization algorithms. It contains a number of implemented algorithms, and basic utility functions for evaluating the implemented algorithms against any image dataset. While currently the framework only includes splicing localization algorithms, it could easily be extended with algorithms localizing other forms of tampering, such as copy-moving.

## Citation

The paper describing the framework and evaluations is the following. Please cite it for all uses of the code:

	@article{zampoglou2017large,
	  title={Large-scale evaluation of splicing localization algorithms for web images},
	  author={Zampoglou, Markos and Papadopoulos, Symeon and Kompatsiaris, Yiannis},
	  journal={Multimedia Tools and Applications},
	  volume={76},
	  number={4},
	  pages={4801--4834},
	  year={2017},
	  publisher={Springer}
	}


For each algorithm implementation you use in your work, you should also cite the corresponding original paper. References can be found in each algorithm's README.txt file.

## Getting started

The framework was tested in MATLAB 8.3.0.532 (R2014a), in both Ubuntu 14.04 and Windows 7. It will most likely also work in older and newer versions of MATLAB. The only requirements are two auxiliary functions, `CleanUpImage.m` and `getAllFiles.m,` both included in the `Util/` subdirectory. Also, certain algorithms require Phil Salee's MATLAB JPEG Toolbox to be in the path. The `Util/` subdirectory includes a version of the Toolbox, alongside compiled .mex files for Windows and Linux x64 architectures, acquired from the [IAPP Research Group home page][]. Make sure all prerequisites are present in your MATLAB path before using the framework.

## Framework structure

The Framework consists of a number of functions used for algorithm evaluation, the `Util` subdirectory, containing auxiliary functions necessary for certain algorithms or evaluation functions, and the `Algorithms/` subdirectory, containing all implemented tampering detection algorithms.

Every algorithm that is part of the Framework follows the same structure: a subdirectory within `Algorithms/`, preferably named after a short abbreviation of the method name (e.g. `Algorithms/ADQ1/` for an Aligned Double Quantization algorithm) that contains all necessary code within, plus an `analyze.m` function that serves as the algorithm API. `analyze.m` should be a function, taking a full-path filename of an image, and returning a 2D tampering localization map. There is no restriction on the size of the map or the value range, although it should be of `Double` type. 

All 14 algorithms currently contained in the Framework also include a demo image and a `demo.m` script which takes the demo image, analyzes it using the algorithm and displays the localization output. All images have been chosen from real-world and experimental data to demonstrate cases of successful application of the corresponding algorithms.

The evaluation functions include the following m-files: `CollectMapStatistics.m`, `CompactCurve.m`, `EvaluateAlgorithm.m`, `ExtractMaps.m`, `OutputFileStatistics.m`, and one image file that serves as a ground-truth mask for untampered files: `PositivesMask.png`.

## Using the framework

The framework is called by setting the options in `EvaluateAlgorithm.m` in order to evaluate one algorithm against one dataset, and then calling the script. The final output is a True Positives vs False Positives curve, and the numeric value of the TP rate of the algorithm for a FP rate of 0.05.

### Algorithm and dataset placement and structure

The algorithm to be evaluated must have a MATLAB implementation placed in `Algorithms\`, and an operational `analyze.m` file inside it. The dataset must consist of a number of tampered images, a number of untampered images and a number of binary tampering localization masks. The tampered and untampered images must be in a separate directory each. There is no problem if any one (or both) are further separated into subdirectories. There are two possibilities for the binary masks. 

  * One is that a different mask exists for each tampered image. In that case, the mask must have a filename identical to the tampered image, and a `.png` extension, regardless of the extension of the original image. If the tampered images are organized in subdirectories, then the mask files should follow an identical subdirectory structure. 
  * The other is that a single mask will exist for the entire dataset. Such is the case with some artificial datasets, such as the one used in [this paper]. In this case, there should be a single .png image at the root of a directory with no other subdirectories or files beside it.

In all cases, to get the binary map the .png images are loaded by matlab, the three chanels are averaged, and then the image is thresholded at pixel value (Max-Min)/2.

### Setting the options

The options to set prior to running `EvaluateAlgorithm.m` are:

* `Options.AlgorithmName:` The name of the algorithm. Must be the name of a subdirectory in `Algorithms\`
* `Options.DatasetName:` The name of the dataset. It is only used for naming the output folders, and does not have to correspond to an existing path.
* `Options.SplicedPath:` The root path where the spliced images of the dataset are placed. There is no problem if they are further divided in subdirectories, but the unspliced images must be placed in a different path
* `Options.AuthenticPath:` The root path where the authentic (unspliced) images of the dataset are placed. The same rules as for spliced images apply.
* `Options.MasksPath:` The root path where the .png binary masks are placed. See the section above on binary mask structure and format.
* `Options.OutputPath:` The path where the intermediate outputs will be saved.
* `Options.ValidExtensions:` The image extensions from the dataset that the framework should take into account. While standard datasets contain images of various formats (PNG, TIFF, JPEG) certain algorithms can only operate on particular image formats (mostly JPEG). If the algorithm under evaluation has such restrictions, the ValidExtensions list should be limited to those


## Frequently asked questions

**Q1: How can I get the algorithm output for a given image?**
When looking at the output for a given image, there are two workspace variables: `BinMask` and `Result`. `BinMask` contains the ground truth mask, and `Result` contains the algorithm output map.

**Q2: Why are the dimensions of the algorithm output map different from the image size?**
The dimensions of the `Result` variable, containing the algorithm output map (or maps) depend on the input image and the algorithm. For example, `ADQ1` returns one value for each 8x8 block, thus the output for an image of size `M x N` is `(M/8) x (N/8)`. In all implemented algorithms, the output map is smaller or equal in size to the input image.

**Q3 Do the tampered / untampered images have to be of a certain size when running the algorithms, or does that not matter? Why is the default ground truth mask for authentic images of size 1024 x 1024?**
There is no issue with the size of the masks. In any case the size discrepancy is inherent in the task since each algorithm outputs a different size mask. During evaluation, the output is resized to the size of the ground truth mask (since we assume the latter is bigger) using nearest neighbour interpolation.
In line 6 of PositivesMask.png:

    ResultMap=imresize(ResultMap,size(Mask),'nearest');

**Q4 Is it possible to add a new algorithm to the toolbox besides the 14 existing ones?**
Yes, the framework is inherently extensible. You need to add a subfolder to the `algorithms` folder. The framework looks into the indicated folder for a function named `analyze.m` that takes as input a single string containing the full path to an image, and returns the algorithm's output map. See the `analyze.m` files in the existing algorithm subfolders as examples. 



  [IAPP Research Group home page]:http://lesc.det.unifi.it/en/node/187
  [this paper]:http://ieeexplore.ieee.org/xpl/login.jsp?tp=&arnumber=6470675&url=http%3A%2F%2Fieeexplore.ieee.org%2Fxpls%2Fabs_all.jsp%3Farnumber%3D6470675
