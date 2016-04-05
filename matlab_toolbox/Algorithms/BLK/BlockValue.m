function b = BlockValue( BLOCK_STRUCT )
    % Get the per-block feature. This is part of our implementation of the
    % algorithm as described in Li, Weihai, Yuan Yuan, and 
    % Nenghai Yu. "Passive detection of doctored JPEG image via block
    % artifact grid extraction." Signal Processing 89, no. 9 (2009):
    % 1821-1829.
    %
    % Copyright (C) 2016 Markos Zampoglou
    % Information Technologies Institute, Centre for Research and Technology Hellas
    % 6th Km Harilaou-Thermis, Thessaloniki 57001, Greece
    
    blockData=BLOCK_STRUCT.data;
    
    Max1=max(sum(blockData(2:7,2:7)));
    Min1=min(sum(blockData(2:7,[1 8])));
    Max2=max(sum(blockData(2:7,2:7),2));
    Min2=min(sum(blockData([1 8],2:7),2));
    
    b=Max1-Min1+Max2-Min2;
    
end