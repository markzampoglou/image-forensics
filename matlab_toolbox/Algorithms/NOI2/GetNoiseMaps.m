function estV = GetNoiseMaps( im, sizeThreshold, filter_type, filter_size, block_rad )
    % Copyright (C) 2016 Markos Zampoglou
    % Information Technologies Institute, Centre for Research and Technology Hellas
    % 6th Km Harilaou-Thermis, Thessaloniki 57001, Greece
    %
    % This code implements the algorithm presented in:
    % Lyu, Siwei, Xunyu Pan, and Xing Zhang. "Exposing region splicing
    % forgeries with blind local noise estimation." International Journal
    % of Computer Vision 110, no. 2 (2014): 202-221. 
    
    % Due to extremely high memory requirements,
    % especially for large images, this function detects large images and
    % runs a memory efficient version of the code, which stores
    % intermediate data to disk (GetNoiseMaps_hdd)
    
    if numel(im)>sizeThreshold
        disp('hdd-based');
        [ estV ] = GetNoiseMaps_hdd( im, filter_type, filter_size, block_rad );
    else
        disp('ram-based');
        [ estV ] = GetNoiseMaps_ram( im, filter_type, filter_size, block_rad );
    end
end

