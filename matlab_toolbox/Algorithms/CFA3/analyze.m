function [F2Map,CFADetected] = analyze( imPath )
    c=parcluster('local');
    % adjust the number of workers to the CPU capabilities.
    c.NumWorkers=4;
    parpool(c,c.NumWorkers);
    
    im=CleanUpImage(imPath);
    [F2Map,CFADetected] = CFATamperDetection_F2(im);
    
    delete(gcp)
end

