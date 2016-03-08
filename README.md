# image-forensics

This is an integrated framework for image forensic analysis. It includes a Java implementation, documented here, and also includes a MATLAB algorithm evaluation framework, including implementations of a large number of algorithms, located in the subdirectory [matlab_toolbox]. The following documentation concerns the Java service, while the Matlab toolbox is documented within the corresponding subdirectory.

The Java framework implements a number of well-established image tampering localization algorithms, plus a number of tools for image metadata extraction and visualization, including GPS localization and the display of embedded thumbnail(s). It includes an API for downloading an image from the web, extracting an integrated forensics report for an image, storing it in a MongoDB collection under a unique hash identifier and returning it upon request.

## Getting started

The library requires Java 8 and MogoDB installed in order to operate. The main API is located in class `api.ReportManagement` and consists of three methods:

* downloadURL
* createReport
* getReport

The first takes as input a URL, downloads the corresponding image and creates an empty database entry with the corresponding hash. The second takes as input a hash, and populates the corresponding database entry with the analysis results. The third method can be called at any time, takes a hash as input, and returns the contents of the corresponding database entry at the time, in the form of a ForensicReport object.


## Downloading an image from a URL

The method requires the image URL (can also be a local `file:/`), the path to an output folder where the image will be stored, and the IP of the MongoDB server:

    String hash = downloadURL("http://some.domain/some_image.jpg", "/home/myhome/DownloadedPics/", "127.0.0.1")

Upon completion, the method returns a string containing the hash of the URL, if the image was downloaded correctly, or the string `"URL_ERROR"` if the URL could not be downloaded. If the process completed successfully, a MongoDB entry is also created, using the Hash as key.

## Building a forensics report

Consecutively, the `createReport` method can be called, in order to populate the report with the output of the various algorithms and features. It requires the hash, the IP of the MongoDB server and the output folder where the images produced by the tampering localization algorithms will be stored:

    createReport(hash, "127.0.0.1", "/home/myhome/DownloadedPics/");

The framework has been written with the aim of being wrapped with a Web service, and thus attention has been paid to maximizing asynchronous operations. To this end, the various operations called by `createReport()` operate asynchronously, with each method updating the corresponding field in the database entry as it completes, without watiting for the other operations to finish.

The framework currently consists of nine distinct operations, organized in an equal number of packages; three of those concern metadata information (package `meta`), while six more concern tampering localization algorithms (package `maps`). All classes have constructors that take a file path pointing to the image file, and store the output in public variable.

## Metadata

### EXIF metadata

The class `MetadataExtractor` is located in package `meta.metadata`. The constructor takes a string as input, containing the full path to the image, and stores the output in a public JsonObject `metadataReport.` The object contains all metadata fields, as extracted and organized by Drew Noakes' [metadata-extractor][] library. 

    MetadataExtractor metaExtractor = new MetadataExtractor("/home/me/pictures/tmp.jpg");
    JsonObject metadata = metaExtractor.metadataReport;

### GPS geolocation

The class `GPSExtractor` is located in package `meta.gps`.

    GPSExtractor gps = new GPSExtractor("/home/me/pictures/tmp.jpg");
    boolean exists = gps.exists;
    double latitude = gps.latitude;
    double longitude = gps.longitude;

### Thumbnail extraction

The class `ThumbnailExtractor` is located in package `meta.thumbnail`. The constructor takes a string as input, containing the full path to the image, and stores the output in a public `List<BufferedImage>` `thumbnails` and a public `int` `numberOfThumbnails`. If no thumbnails are found, the list is `null` and the number of thubnails equals zero. 

    ThumbnailExtractor thumbnailExtractor = new ThumbnailExtractor("/home/me/pictures/tmp.jpg");
    List<BufferedImage> thumbnailList = thumbnailExtractor.thumbnails;
    int numberOfThumbnails = thumbnailExtractor.numberOfThumbnails;

## Image tampering localization

### Double JPEG Quantization

This is a Java implementation of the algorithm described in:
* Lin, Zhouchen, Junfeng He, Xiaoou Tang, and Chi-Keung Tang. "Fast, automatic and fine-grained tampered JPEG image detection via DCT coefficient analysis." Pattern Recognition 42, no. 11 (2009): 2492-2501.

The class `DQExtractor` is located in package `maps.dq` and, like all other extractors, has a constructor that takes the image file path as a string. It returns a buffered image containing the analysis output, as well as the output's maximum and minimum probability values.

    DQExtractor dq = new DQExtractor("/home/me/pictures/tmp.jpg");
    BufferedImage dqMap = dq.displaySurface;
    double minProbValue = dq.minProbValue;
    double maxProbValue = dq.maxProbValue;


### JPEG Ghosts

This is a Java implementation of the algorithm described in:
* Farid, Hany. "Exposing digital forgeries from JPEG ghosts." Information Forensics and Security, IEEE Transactions on 4, no. 1 (2009): 154-160.

The class `GhostExtractor` is located in package `maps.ghost` and returns a List of output images, the different JPEG quality levels these images correspond to, and their minimum and maximum values.

    GhostExtractor ghostExtractor = new GhostExtractor(sourceFile, maxImageSmallDimension, numThreads);
    List<Integer> qualities = ghostExtractor.ghostQualities;
    List<Float> differences = ghostExtractor.allDifferences;
    List<Float> minValues = ghostExtractor.ghostMin;
    List<Float> maxValues = ghostExtractor.ghostMax;
    int minQuality = ghostExtractor.qualityMin;
    int maxQuality = ghostExtractor.qualityMax;
    ghostReport.maxQuality = ghostExtractor.qualityMax;
    List<String> maps = ghostExtractor.ghostMaps;

### JPEG Blocking Artifact Inconsistencies

This is a Java implementation of the algorithm described in:
* Li, Weihai, Yuan Yuan, and Nenghai Yu. "Passive detection of doctored JPEG image via block artifact grid extraction." Signal Processing 89, no. 9 (2009): 1821-1829.

The class `BlockingExtractor` is located in package `maps.blocking` and has a constructor that takes the image file path as a string. It returns a buffered image containing the analysis output, as well as the output's maximum and minimum values.

    BlockingExtractor blk = new BlockingExtractor("/home/me/pictures/tmp.jpg");
    BufferedImage blockingMap = blk.displaySurface;
    double blkmin = blk.blkmin;
    double blkmax = blk.blkmax;

### Error Level Analysis

This is a Java implementation of the Error Level Analysis algorithm. ELA is probably the most well known and widely used tampering detection method, with Web-based implementations offered by [FotoForensics][] and [Forensically][]

The class `ELAExtractor` is located in package `maps.ela` and has a constructor that takes the image file path as a string. It returns a buffered image containing the analysis output, as well as the output's maximum and minimum values.

    ELAExtractor ela = new ELAExtractor("/home/me/pictures/tmp.jpg");
    BufferedImage elaMap = ela.displaySurface;
    double elaMin = ela.elaMin;
    double elaMin = ela.elaMin;

### Discrete Wavelet High-Frequenct Noise Variance

This is a Java implementation of the algorithm described in:
* Li, Weihai, Yuan Yuan, and Nenghai Yu. "Passive detection of doctored JPEG image via block artifact grid extraction." Signal Processing 89, no. 9 (2009): 1821-1829.

The class `DWNoiseVarExtractor` is located in package `maps.dwnoisevar` and has a constructor that takes the image file path as a string. It returns a buffered image containing the analysis output, as well as the output's maximum and minimum values.

    DWNoiseVarExtractor dwNoise = new DWNoiseVarExtractor("/home/me/pictures/tmp.jpg");
    BufferedImage dwNoiseMap = dwNoise.displaySurface;
    double maxNoiseValue = dwNoise.maxNoiseValue;
    double maxNoiseValue = dwNoise.maxNoiseValue;

### Median Filtering Noise Residue

This is a Java implementation of the Median Filtering Noise Residue algorithm featured in the Web-based image forensics service [Forensically]. The class `MedianNoiseExtractor` is located in package `maps.mediannoise` and has a constructor that takes the image file path as a string. It returns a buffered image containing the analysis output, as well as the output's maximum and minimum values.

    MedianNoiseExtractor medianNoise = new MedianNoiseExtractor("/home/me/pictures/tmp.jpg");
    BufferedImage medianNoiseMap = medianNoise.displaySurface;

## Pulling a report from the database

At any step of the report extraction process, we can pull the report with whatever fields have been completed, using the `getReport` method from `api.ReportManagement`

    ForensicReport forensicReport = getReport(hash, "127.0.0.1");

This returns a `ForensicReport` object containing all the completed elements of the report. All metadata and localization fields contain a `boolean` variable named `completed` whose aim is to indicate if the corresponding method has concluded its computations and thus can be displayed.


  [metadata-extractor]: https://drewnoakes.com/code/exif/
  [FotoForensics]:http://fotoforensics.com/
  [Forensically]:https://29a.ch/photo-forensics/#forensic-magnifier
  [matlab_toolbox]:https://github.com/MKLab-ITI/image-forensics/tree/master/matlab_toolbox
