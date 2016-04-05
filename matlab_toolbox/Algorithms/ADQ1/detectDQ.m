function [OutputMap, Feature_Vector, coeffArray] = detectDQ( im )
    % Copyright (C) 2016 Markos Zampoglou
    % Information Technologies Institute, Centre for Research and Technology Hellas
    % 6th Km Harilaou-Thermis, Thessaloniki 57001, Greece
    %
    % This code implements the algorithm presented in:
    % Lin, Zhouchen, Junfeng He, Xiaoou Tang, and Chi-Keung Tang. "Fast, automatic
    % and fine-grained tampered JPEG image detection via DCT coefficient analysis."
    % Pattern Recognition 42, no. 11 (2009): 2492-2501.
    %
    % Depending on whether im was created using jpeg_read (and thus is a struct) 
    % or CleanUpImage(/imread), call a different version of the algorithm.
    % jpeg_read produces more robust results, but can only open
    % JPEG-compressed images
    
    if isstruct(im)
        [OutputMap, Feature_Vector, coeffArray] = detectDQ_JPEG( im );
    else
        [OutputMap, Feature_Vector, coeffArray] = detectDQ_NonJPEG( im );
    end
    
    
end

