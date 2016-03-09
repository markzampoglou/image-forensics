% Copyright (C) 2016 Markos Zampoglou
% Information Technologies Institute, Centre for Research and Technology Hellas
% 6th Km Harilaou-Thermis, Thessaloniki 57001, Greece

close all; clear all;
im1='demo.jpg'
subplot(2,2,1);
imshow(CleanUpImage(im1));
subplot(2,2,3);
[OutputMap, Feature_Vector, coeffArray] = analyze(im1);
imagesc(OutputMap);
title('JPG');
subplot(2,2,4);
im2='demo.png'
[OutputMap, Feature_Vector, coeffArray] = analyze(im2);
imagesc(OutputMap);
title('PNG');