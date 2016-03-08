function [OutputMap] = analyze( imPath )
    %ANALYZE Summary of this function goes here
    %   Detailed explanation goes here
    try
        %using evalc to suppress output
        %using try_catch instead of just checking the extension to also
        %catch jpeg images with wrong extension
        [~,im] = evalc('jpeg_read(imPath);');
    catch
        im=CleanUpImage(imPath);
    end
    OutputMap = GetBlockArtifact(im);
end

