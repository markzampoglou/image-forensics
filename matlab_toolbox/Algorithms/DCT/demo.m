

close all; clear all;
im1='demo.jpg'
[OutputMap] = analyze(im1);
imagesc(OutputMap);
title('JPG');
figure;
im2='demo.png'
[OutputMap] = analyze(im2);
imagesc(OutputMap);
title('PNG');