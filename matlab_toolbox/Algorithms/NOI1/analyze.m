function OutputMap = analyze( imPath )
    BlockSize=8;
    
    im=CleanUpImage(imPath);
    
    OutputMap = GetNoiseMap(im, BlockSize);
end