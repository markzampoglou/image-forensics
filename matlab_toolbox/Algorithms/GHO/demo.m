close all; clear all;
im1='demo.jpg'
OutputMap = analyze(im1);
for ii=1:length(OutputMap)
    imagesc(OutputMap{ii});
    pause;
end