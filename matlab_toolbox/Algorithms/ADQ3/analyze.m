function [OutputMap] = analyze( imPath )
    %ANALYZE Summary of this function goes here
    %   Detailed explanation goes here

    im=jpeg_read(imPath);
    OutputMap = BenfordDQ(im);
end