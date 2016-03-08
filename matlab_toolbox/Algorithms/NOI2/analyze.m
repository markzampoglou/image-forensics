function OutputMap = analyze( imPath )
    % the algorithm has very high memory requirements. This is the maximum
    % number of elements (M*N*3) in an image for which we use a RAM-based
    % implementation. Above that, we switch to a slower, HDD-based method
    % with lower memory requirements. Even then, it is highly likely that
    % the system memory may be insufficient.
    % The current value was used in our experiments on a 16GB RAM system.
    sizeThreshold=55*(2^20);
    
    filter_size = 4; % size of the pre-smoothing filters
    block_rad = 8; %size of sliding window -2 4 and 8 are all reasonable
    filter_type = 'rand'; %	the type of band-pass filter used 
                         %  supported types, "dct", "haar", "rand"
    im=CleanUpImage(imPath);
    OutputMap = GetNoiseMaps(im, sizeThreshold, filter_type, filter_size, block_rad);
end