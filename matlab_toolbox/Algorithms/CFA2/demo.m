close all; clear all;
im1='demo.jpg'
[OutputMap, Feature_Vector, coeffArray] = analyze(im1);
imagesc(OutputMap);
