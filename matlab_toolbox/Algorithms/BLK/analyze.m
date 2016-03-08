function [OutputMap] = analyze( imPath )
    %ANALYZE Summary of this function goes here
    %   Detailed explanation goes here

    im=CleanUpImage(imPath);
    
    %The original code provided by the authors is the following (included 
    %for comparison):
    %OutputMap = block(im);

    
    %Markos Zampoglou: wrote a new implementation which is significantly 
    %faster with negligible differences in the output.
    OutputMap = GetBlockGrid(im);
end