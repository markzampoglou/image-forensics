function [OutputMap, OutputMap_Quant] = analyze( imPath )
    % OutputMap_Quant returns a binarized version of OutputMap
    im=CleanUpImage(imPath);
    
    [OutputMap, OutputMap_Quant] = PCANoise(im);
end