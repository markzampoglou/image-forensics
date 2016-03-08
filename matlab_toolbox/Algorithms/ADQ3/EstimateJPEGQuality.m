function [ Quality ] = EstimateJPEGQuality( imIn )
    %ESTIMATEJPEGQUALITY Summary of this function goes here
    %   Detailed explanation goes here
    if length(imIn.quant_tables)==1
        imIn.quant_tables{2}=imIn.quant_tables{1};
    end
    YQuality=100-(sum(sum(imIn.quant_tables{1}))-imIn.quant_tables{1}(1,1))/63;
    CrCbQuality=100-(sum(sum(imIn.quant_tables{2}))-imIn.quant_tables{1}(1,1))/63;
    
    Diff= abs(YQuality-CrCbQuality)*0.98; %why is this better
    Quality = (YQuality+2*CrCbQuality)/3.0 + Diff;
end