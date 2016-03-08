function OutputMap = MedFiltForensics( ImIn, NSize, Multiplier, Flatten)
    %ELA Summary of this function goes here
    %   Detailed explanation goes here
    
    if nargin<4
        Flatten=false;
    end
    if nargin<3
        Multiplier=10;
    end
    if nargin<2
        NSize=3;
    end
    
    for Channel=1:3
        ImMed(:,:,Channel)=medfilt2(double(ImIn(:,:,Channel)),[NSize NSize]);
    end
    
    OutputMap=(abs(double(ImIn)-double(ImMed))*Multiplier);
    
    
    if Flatten
        OutputMap=mean(OutputMap,3);
    end
end

