function [OutputMap] = analyze( imPath )
    %ANALYZE Summary of this function goes here
    %   Detailed explanation goes here
    im=CleanUpImage(imPath);
    checkDisplacements=0;
    smoothFactor=1;
    [OutputX, OutputY, dispImages, imin, Qualities, Mins]=Ghost(im, checkDisplacements);
    OutputMap = dispImages;
end

