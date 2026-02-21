module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = ">= 19.0.0"

   name    = var.name
   kubernetes_version = var.cluster_version
  
  vpc_id                         = var.vpc_id
  subnet_ids                     = var.subnet_ids
  control_plane_subnet_ids       = var.subnet_ids
  
  endpoint_private_access = false
  endpoint_public_access = true

  enable_irsa = true

  # EKS Managed Node Group(s)
  eks_managed_node_groups = {
    for ng_name, ng in var.node_groups :
    ng_name => {
      desired_size   = ng.desired_capacity
      min_size       = ng.min_capacity
      max_size       = ng.max_capacity
      instance_types = ng.instance_types
      
      key_name = ng.key_name == "" ? null : ng.key_name
      
      subnet_ids = var.subnet_ids
      
      tags = merge(var.tags, ng.additional_tags)
    }
  }

  tags = merge(var.tags, {
    "Name" = var.name
  })
}