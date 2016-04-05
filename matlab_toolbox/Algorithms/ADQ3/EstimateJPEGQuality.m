function [ Quality ] = EstimateJPEGQuality( imIn )
    % This function estimates the quality of a JPEG image that has been
    % loaded using "jpeg_read" from the MATLAB JPEG Toolbox. The algorithm
    % is taken from http://fotoforensics.com/tutorial-estq.php
    %
    % Copyright (C) 2016 Markos Zampoglou
    % Information Technologies Institute, Centre for Research and Technology Hellas
    % 6th Km Harilaou-Thermis, Thessaloniki 57001, Greece
    
    if length(imIn.quant_tables)==1
        imIn.quant_tables{2}=imIn.quant_tables{1};
    end
    YQuality=100-(sum(sum(imIn.quant_tables{1}))-imIn.quant_tables{1}(1,1))/63;
    CrCbQuality=100-(sum(sum(imIn.quant_tables{2}))-imIn.quant_tables{1}(1,1))/63;
    
    Diff= abs(YQuality-CrCbQuality)*0.98;
    Quality = (YQuality+2*CrCbQuality)/3.0 + Diff;
end