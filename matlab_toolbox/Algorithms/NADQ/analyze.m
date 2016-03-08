function [ OutputMap ] = analyze( imPath )
    %ANALYZE Summary of this function goes here
    %   Detailed explanation goes here
    im=jpeg_read(imPath);
    c2 = 6;
    
    [LLRmap, LLRmap_s, q1table, k1e, k2e, alphat] = getJmapNA_EM(im, 1, c2);
    OutputMap = imfilter(sum(LLRmap,3), ones(3), 'symmetric', 'same');
end

