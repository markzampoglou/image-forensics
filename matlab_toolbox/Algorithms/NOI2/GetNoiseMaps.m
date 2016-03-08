function estV = GetNoiseMaps( im, sizeThreshold, filter_type, filter_size, block_rad )
    if numel(im)>sizeThreshold
        disp('hdd-based');
        [ estV ] = GetNoiseMaps_hdd( im, filter_type, filter_size, block_rad );
    else
        disp('ram-based');
        [ estV ] = GetNoiseMaps_ram( im, filter_type, filter_size, block_rad );
    end
end

