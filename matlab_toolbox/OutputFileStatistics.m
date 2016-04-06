function [ Results ] = OutputFileStatistics( InputStruct )
    ResultMap=InputStruct.Result;
    Mask=InputStruct.BinMask;
    ResultMap(isnan(ResultMap))=0;
    ResultMap(isinf(ResultMap))=max(ResultMap(~isinf(ResultMap)));
    ResultMap=imresize(ResultMap,size(Mask),'nearest');
    
    %Median under and outside mask
    Results.MaskMedian=median(ResultMap(Mask));
    Results.OutsideMedian=median(ResultMap(~Mask));
    
    %Mean under and outside mask
    Results.MaskMean=mean(ResultMap(Mask));
    Results.OutsideMean=mean(ResultMap(~Mask));
    
    if isnan(Results.MaskMedian) || isnan(Results.OutsideMedian) || isnan(Results.MaskMean) | isnan(Results.OutsideMean)
        disp('NaN found. Possibly the ground truth mask is uniform.');
        figure(1);
        imagesc(ResultMap);
        figure(2);
        imagesc(Mask);
        pause
    end
    
    %K-S statistic
    MinValue=min(min(ResultMap));
    MaxValue=max(max(ResultMap));
    HistBinEdges=[MinValue:(MaxValue-MinValue)/20:MaxValue inf];
    HistBinEdges=HistBinEdges(1:end-1);
    Results.MaskHist=histc(ResultMap(Mask),HistBinEdges);
    Results.OutsideHist=histc(ResultMap(~Mask),HistBinEdges);
    if isempty(Results.MaskHist)
        Results.MaskHist=1;
    end
    if isempty(Results.OutsideHist)
        Results.OutsideHist=1;
    end
    Results.MaskHist=Results.MaskHist/sum(Results.MaskHist);
    Results.OutsideHist=Results.OutsideHist/sum(Results.OutsideHist);
    Results.KSStat=max(abs(cumsum(Results.MaskHist(:))-cumsum(Results.OutsideHist(:))));
end

