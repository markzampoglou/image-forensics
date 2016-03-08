function [OutputMap] = analyze( imPath )
    %ANALYZE Summary of this function goes here
    %   Detailed explanation goes here
    im=CleanUpImage(imPath);
    % dimension of statistics. Smaller values work as well
    Nb = 8;
    % number of cumulated bloks
    Ns = 1;
    ImageIn=CleanUpImage(imPath);
    % crop image back to 2x2 blocks, if any dimension is odd
    toCrop=mod(size(ImageIn),2);
    ImageIn=ImageIn(1:end-toCrop(1),1:end-toCrop(2),:);
    % estimate the Bayer array
    [bayer, F1]=GetCFAArray(ImageIn);
    [OutputMap, stat] = CFAloc(ImageIn, bayer, Nb,Ns);
end