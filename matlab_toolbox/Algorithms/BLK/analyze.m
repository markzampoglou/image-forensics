function [OutputMap] = analyze( imPath )
    % Copyright (C) 2016 Markos Zampoglou
    % Information Technologies Institute, Centre for Research and Technology Hellas
    % 6th Km Harilaou-Thermis, Thessaloniki 57001, Greece

    im=CleanUpImage(imPath);
    
    %The original code provided by the authors is the following (included 
    %for comparison):
    %OutputMap = block(im);

    
    %Markos Zampoglou: wrote a new implementation which is significantly 
    %faster with negligible differences in the output.
    OutputMap = GetBlockGrid(im);
end
