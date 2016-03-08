function [OutputMap] = analyze( imPath )
    %ANALYZE Summary of this function goes here
    %   Detailed explanation goes here
    im=CleanUpImage(imPath);
    Quality=90;
    Multiplier=15;
    Flatten=true;
    
    OutputMap = ELA(im,Quality,Multiplier,Flatten);
end

