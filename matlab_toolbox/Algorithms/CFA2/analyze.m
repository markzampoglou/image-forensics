function [F1Map,CFADetected, F1] = analyze( imPath )
    im=CleanUpImage(imPath);
    [F1Map,CFADetected, F1] = CFATamperDetection_F1(im);
end

