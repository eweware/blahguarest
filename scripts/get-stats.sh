# Fetch stats files
scp -i ~/dev/apps/blahguaservice/blahgua_key.pem  ec2-user@ec2-107-22-60-213.compute-1.amazonaws.com:/home/ec2-user/stats/blah-strength.data .
scp -i ~/dev/apps/blahguaservice/blahgua_key.pem  ec2-user@ec2-107-22-60-213.compute-1.amazonaws.com:/home/ec2-user/stats/recent-blah-strength.data .

# R: to plot and save the results in a file:
# library(clusterSim)
# x <- scan("blah-strength.data", cbind(0))
# hist(sort(data.Normalization(x, type="n4")), xlab="Strength", main="Blah Strength Histogram", freq=FALSE)
