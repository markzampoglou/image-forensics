function [ Map ] = GetNoiseMap( im , BlockSize)
    % Copyright (C) 2016 Markos Zampoglou
    % Information Technologies Institute, Centre for Research and Technology Hellas
    % 6th Km Harilaou-Thermis, Thessaloniki 57001, Greece
    %
    % This code implements the algorithm presented in:
    % Mahdian, Babak, and Stanislav Saic. "Using noise inconsistencies for
    % blind image forensics." Image and Vision Computing 27, no. 10 (2009):
    % 1497-1503.
    %
    % BlockSize: the block size for noise variance estimation. Too small
    % reduces quality, too large reduces localization accuracy
    
    
    YCbCr=double(rgb2ycbcr(im));
    Y=YCbCr(:,:,1);
    
    [cA1,cH,cV,cD] = dwt2(Y,'db8');
    
    cD=cD(1:floor(size(cD,1)/BlockSize)*BlockSize,1:floor(size(cD,2)/BlockSize)*BlockSize);
    Block=zeros(floor(size(cD,1)/BlockSize),floor(size(cD,2)/BlockSize),BlockSize.^2);
    
    for ii=1:BlockSize:size(cD,1)-1
        for jj=1:BlockSize:size(cD,2)-1
            blockElements=cD(ii:ii+BlockSize-1,jj:jj+BlockSize-1);
            Block((ii-1)/BlockSize+1,(jj-1)/BlockSize+1,:)=reshape(blockElements,[1 1 numel(blockElements)]);
        end
    end
    
    Map=median(abs(Block),3)./0.6745;    
end