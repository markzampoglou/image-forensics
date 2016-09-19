# image-forensics

This is an integrated framework for image forensic analysis. It includes a Java webservice, including seven splicing detection algorithm implementations, plus additional forensic tools, located in the subdirectory [java_service] and a Matlab algorithm evaluation framework, including implementations of a a very large number of splicing detection algorithms, located in the subdirectory [matlab_toolbox].

## Citations

Please cite the following paper in your publications if you use the Java implementations:

    @inproceedings{zamp16,
      author = "Markos Zampoglou and Symeon Papadopoulos and Yiannis Kompatsiaris and Ruben Bouwmeester and Jochen Spangenberg",
      booktitle = "Social Media In the NewsRoom, {#SMNews16@CWSM}, Tenth International AAAI Conference on Web and Social Media workshops",
      title = "Web and Social Media Image Forensics for News Professionals",
      year = "2016",
    }

If you use the Matlab implementations, use the following citation:

    @article{zampAcc,
      author = "Markos Zampoglou and Symeon Papadopoulos and Yiannis Kompatsiaris",
      title = "A Large-Scale Evaluation of Splicing Localization Algorithms for Web Images",
      journal = "Multimedia Tools and Applications",
      doi = "10.1007/s11042-016-3795-2"
      pages= "Accepted for publication",
    }

In either case, you must also cite the original algorithm publication. The README file within each Matlab algorithm subfolder contains the corresponding citation.

Contact Markos Zampoglou <markzampoglou@iti.gr> for any further information.

  [matlab_toolbox]:https://github.com/MKLab-ITI/image-forensics/tree/master/matlab_toolbox
  [java_service]:https://github.com/MKLab-ITI/image-forensics/tree/master/java_service
