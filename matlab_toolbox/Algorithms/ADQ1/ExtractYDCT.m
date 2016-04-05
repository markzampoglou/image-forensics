function YDCT = ExtractYDCT( im )
    % Copyright (C) 2016 Markos Zampoglou
    % Information Technologies Institute, Centre for Research and Technology Hellas
    % 6th Km Harilaou-Thermis, Thessaloniki 57001, Greece
    %
    % Use this function to get the DCT coefficients for files not stored in
    % JPEG format.
    
    im=double(im);
    
    Y=0.299*im(:,:,1)+0.587*im(:,:,2)+0.114*im(:,:,3);
    Y=Y(:,:,1);
    Y=Y(1:floor(end/8)*8,1:floor(end/8)*8);
    Y=Y-128;
    
    %T = dctmtx(8);
    %dct = @(block_struct) T * block_struct.data * T';
    %YDCT=round(blockproc(Y,[8 8],dct));
    %Use the command below instead of the one above, it's a tiny bit closer
    %to original JPEG DCT
    YDCT=round(bdct(Y,8));
end