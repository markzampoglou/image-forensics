function estV = GetNoiseMaps_hdd( im, filter_type, filter_size, block_rad )
    % Markos Zampoglou: This a variant version of the code, which calls
    % localNoiVarEstimate_hdd, a version in which intermediate data are
    % stored on disk
    
    im=double(rgb2ycbcr(im));
    im=im(:,:,1);
    
    flt = ones(filter_size,1);
    flt = flt*flt'/filter_size^2;
    noiIm = conv2(im,flt,'same');
    
    estV_tmp = localNoiVarEstimate_hdd(noiIm, filter_type, filter_size, block_rad);
    estV = imresize(single(estV_tmp),round(size(estV_tmp)/4),'method','box');
    
    estV(estV<=0.001)=single(mean(mean(mean(estV))));
end

