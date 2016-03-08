
close all; clear all;
im1='demo.jpg'
[OutputMap, Feature_Vector, coeffArray] = analyze(im1);
imagesc(OutputMap);
title('JPG');
figure;
im2='demo.png'
[OutputMap, Feature_Vector, coeffArray] = analyze(im2);
imagesc(OutputMap);
title('PNG');