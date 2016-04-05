function OutputMap = MedFiltForensics( ImIn, NSize, Multiplier, Flatten)
    % Copyright (C) 2016 Markos Zampoglou
    % Information Technologies Institute, Centre for Research and Technology Hellas
    % 6th Km Harilaou-Thermis, Thessaloniki 57001, Greece
    %
    % This code implements a median filter residue forensics algorithm,
    % similar to the one used in Forensically
    % (https://29a.ch/photo-forensics/)
    
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

