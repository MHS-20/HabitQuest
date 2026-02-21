# check aws login
aws sts get-caller-identity

# get eks access
aws eks update-kubeconfig --region eu-central-1 --name my-eks-cluster