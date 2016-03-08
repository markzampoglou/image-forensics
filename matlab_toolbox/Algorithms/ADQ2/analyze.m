function [ OutputMap ] = analyze( imPath )
    %ANALYZE Summary of this function goes here
    %   Detailed explanation goes here
    im=jpeg_read(imPath);
    ncomp = 1;
    c1 = 1;
    c2 = 15;
    
    OutputMap = getJmap(im,ncomp,c1,c2);
end

