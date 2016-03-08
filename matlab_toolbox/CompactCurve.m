function [ CompactCurves ] = CompactCurve( NewSeries,  TmpRange)
    Term=size(NewSeries,2)-2;
    Ind=1;
    while Ind<=Term
        if round(NewSeries(1,Ind)*100)==round(NewSeries(1,Ind+1)*100)
            NewSeries=NewSeries(:,[1:Ind Ind+2:end]);
            TmpRange=TmpRange([1:Ind Ind+2:end]);
        else
            Ind=Ind+1;
        end
        Term=size(NewSeries,2)-2;
    end
    if round(NewSeries(1,end)*100)==round(NewSeries(1,end-1)*100)
        NewSeries=NewSeries(:,1:end-1);
        TmpRange=TmpRange(1:end-1);
    end
    
    CompactCurves=[TmpRange;NewSeries];
    
end

